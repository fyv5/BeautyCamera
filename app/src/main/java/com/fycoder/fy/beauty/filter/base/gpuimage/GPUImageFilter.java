/*
 * Copyright (C) 2012 CyberAgent
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.fycoder.fy.beauty.filter.base.gpuimage;

import android.graphics.PointF;
import android.opengl.GLES20;


import com.fycoder.fy.beauty.utils.OpenGLUtils;
import com.fycoder.fy.beauty.utils.Rotation;
import com.fycoder.fy.beauty.utils.TextureRotationUtil;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.util.LinkedList;

public class GPUImageFilter {
    //顶点着色器 设置顶点的位置，并且把位置和纹理坐标这样的参数发送到片段着色器。
    public static final String NO_FILTER_VERTEX_SHADER = "" +
            "attribute vec4 position;\n" +
            //position变量是我们在程序中传给Shader的顶点数据的位置，是一个矩阵，规定了图像4个点的位置，
            // 并且可以在shader中经过矩阵进行平移、旋转等再次变换。在GPUImage中，我们根据GLSurfaceView的大小、
            // PreviewSize的大小实现计算出矩阵，通过glGetAttribLocation获取id，再通过glVertexAttribPointer将矩阵传入。
            // 新的顶点位置通过在顶点着色器中写入gl_Position传递到渲染管线的后继阶段继续处理。
            // 结合后面绘制过程中的glDrawArrays(GL_TRIANGLE_STRIP, 0, 4);，首先选取第三个点，与前两个点绘制成一个三角形，
            // 再选取最后一个点，与第二、第三个点绘制成三角形，最终绘制成多边形区域。
            "attribute vec4 inputTextureCoordinate;\n" +
            //inputTextureCoordinate是纹理坐标，纹理坐标定义了图像的哪一部分将被映射到多边形
            " \n" +
            "varying vec2 textureCoordinate;\n" +
            //因为顶点着色器负责和片段着色器交流，所以我们需要创建一个变量和它共享相关的信息。
            // 在图像处理中，片段着色器需要的唯一相关信息就是顶点着色器现在正在处理哪个像素
            " \n" +
            "void main()\n" +
            "{\n" +
            "    gl_Position = position;\n" +
            //gl_Position是用来传输投影坐标系内顶点坐标的内建变量，GPUImage在Java层已经变换过，在这里不需要经过任何变换
            "    textureCoordinate = inputTextureCoordinate.xy;\n" +
            //取出这个顶点中纹理坐标的 X 和 Y 的位置（仅需要这两个属性），然后赋值给一个将要和片段着色器通信的变量
            "}";
    //片段着色器 唯一目的就是确定一个像素的颜色
    public static final String NO_FILTER_FRAGMENT_SHADER = "" +
            "varying highp vec2 textureCoordinate;\n" +
            //对应顶点着色器中变量名相同的变量，片段着色器作用在每一个像素上，我们需要一个方法来确定我们当前在分析
            //哪一个像素/片段。它需要存储像素的 X 和 Y 坐标。我们接收到的是当前在顶点着色器被设置好的纹理坐标。
            " \n" +
            "uniform sampler2D inputImageTexture;\n" +
            //uniforms变量(一致变量)用来将数据值从应用程其序传递到顶点着色器或者片元着色器
            " \n" +
            "void main()\n" +
            "{\n" +
            //创建一个 2D 的纹理。它采用我们之前声明过的属性作为参数来决定被处理的像素的颜色
            "     gl_FragColor = texture2D(inputImageTexture, textureCoordinate);\n" +
            "}";

    private final LinkedList<Runnable> mRunOnDraw;
    private final String mVertexShader;
    private final String mFragmentShader;
    protected int mGLProgId;
    protected int mGLAttribPosition;
    protected int mGLUniformTexture;
    protected int mGLAttribTextureCoordinate;
    protected int mGLStrengthLocation;
    protected int mOutputWidth;
    protected int mOutputHeight;
    protected boolean mIsInitialized;
    protected FloatBuffer mGLCubeBuffer;
    protected FloatBuffer mGLTextureBuffer;
    protected int mSurfaceWidth, mSurfaceHeight;
    
    public GPUImageFilter() {
        this(NO_FILTER_VERTEX_SHADER, NO_FILTER_FRAGMENT_SHADER);
    }

    public GPUImageFilter(final String vertexShader, final String fragmentShader) {
        mRunOnDraw = new LinkedList<Runnable>();
        mVertexShader = vertexShader;
        mFragmentShader = fragmentShader;
        
        mGLCubeBuffer = ByteBuffer.allocateDirect(TextureRotationUtil.CUBE.length * 4)
                .order(ByteOrder.nativeOrder())
                .asFloatBuffer();
        mGLCubeBuffer.put(TextureRotationUtil.CUBE).position(0);

        mGLTextureBuffer = ByteBuffer.allocateDirect(TextureRotationUtil.TEXTURE_NO_ROTATION.length * 4)
                .order(ByteOrder.nativeOrder())
                .asFloatBuffer();
        mGLTextureBuffer.put(TextureRotationUtil.getRotation(Rotation.NORMAL, false, true)).position(0);
    }

    public void init() {
        onInit();
        mIsInitialized = true;
        onInitialized();
    }

    protected void onInit() {
        mGLProgId = OpenGLUtils.loadProgram(mVertexShader, mFragmentShader);
        mGLAttribPosition = GLES20.glGetAttribLocation(mGLProgId, "position");
        mGLUniformTexture = GLES20.glGetUniformLocation(mGLProgId, "inputImageTexture");
        mGLAttribTextureCoordinate = GLES20.glGetAttribLocation(mGLProgId,
                "inputTextureCoordinate");
        mGLStrengthLocation = GLES20.glGetUniformLocation(mGLProgId,
                "strength");
        mIsInitialized = true;
    }

    protected void onInitialized() {
    	setFloat(mGLStrengthLocation, 1.0f);
    }

    public final void destroy() {
        mIsInitialized = false;
        GLES20.glDeleteProgram(mGLProgId);
        onDestroy();
    }

    protected void onDestroy() {
    }

    public void onOutputSizeChanged(final int width, final int height) {
        mOutputWidth = width;
        mOutputHeight = height;
    }

    public int onDrawFrame(final int textureId, final FloatBuffer cubeBuffer,
                       final FloatBuffer textureBuffer) {
        GLES20.glUseProgram(mGLProgId);
        runPendingOnDrawTasks();
        if (!mIsInitialized) {
            return OpenGLUtils.NOT_INIT;
        }

        cubeBuffer.position(0);
        GLES20.glVertexAttribPointer(mGLAttribPosition, 2, GLES20.GL_FLOAT, false, 0, cubeBuffer);
        GLES20.glEnableVertexAttribArray(mGLAttribPosition);
        textureBuffer.position(0);
        GLES20.glVertexAttribPointer(mGLAttribTextureCoordinate, 2, GLES20.GL_FLOAT, false, 0,
                textureBuffer);
        GLES20.glEnableVertexAttribArray(mGLAttribTextureCoordinate);
        if (textureId != OpenGLUtils.NO_TEXTURE) {
            GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureId);
            GLES20.glUniform1i(mGLUniformTexture, 0);
        }
        onDrawArraysPre();
        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4);
        GLES20.glDisableVertexAttribArray(mGLAttribPosition);
        GLES20.glDisableVertexAttribArray(mGLAttribTextureCoordinate);
        onDrawArraysAfter();
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 0);
        return OpenGLUtils.ON_DRAWN;
    }
    
    public int onDrawFrame(final int textureId) {
		GLES20.glUseProgram(mGLProgId);
		runPendingOnDrawTasks();
		if (!mIsInitialized) 
			return OpenGLUtils.NOT_INIT;
		
		mGLCubeBuffer.position(0);
		GLES20.glVertexAttribPointer(mGLAttribPosition, 2, GLES20.GL_FLOAT, false, 0, mGLCubeBuffer);
		GLES20.glEnableVertexAttribArray(mGLAttribPosition);
		mGLTextureBuffer.position(0);
		GLES20.glVertexAttribPointer(mGLAttribTextureCoordinate, 2, GLES20.GL_FLOAT, false, 0,
		     mGLTextureBuffer);
		GLES20.glEnableVertexAttribArray(mGLAttribTextureCoordinate);
		if (textureId != OpenGLUtils.NO_TEXTURE) {
		 GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
		 GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureId);
		 GLES20.glUniform1i(mGLUniformTexture, 0);
		}
		onDrawArraysPre();
		GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4);
		GLES20.glDisableVertexAttribArray(mGLAttribPosition);
		GLES20.glDisableVertexAttribArray(mGLAttribTextureCoordinate);
		onDrawArraysAfter();
		GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 0);
		return OpenGLUtils.ON_DRAWN;
	}
    
    protected void onDrawArraysPre() {}
    protected void onDrawArraysAfter() {}
    
    protected void runPendingOnDrawTasks() {
        while (!mRunOnDraw.isEmpty()) {
            mRunOnDraw.removeFirst().run();
        }
    }

    public boolean isInitialized() {
        return mIsInitialized;
    }

    public int getOutputWidth() {
        return mOutputWidth;
    }

    public int getOutputHeight() {
        return mOutputHeight;
    }

    public int getProgram() {
        return mGLProgId;
    }

    public int getAttribPosition() {
        return mGLAttribPosition;
    }

    public int getAttribTextureCoordinate() {
        return mGLAttribTextureCoordinate;
    }

    public int getUniformTexture() {
        return mGLUniformTexture;
    }

    protected void setInteger(final int location, final int intValue) {
        runOnDraw(new Runnable() {
            @Override
            public void run() {
                GLES20.glUniform1i(location, intValue);
            }
        });
    }

    protected void setFloat(final int location, final float floatValue) {
        runOnDraw(new Runnable() {
            @Override
            public void run() {
                GLES20.glUniform1f(location, floatValue);
            }
        });
    }

    protected void setFloatVec2(final int location, final float[] arrayValue) {
        runOnDraw(new Runnable() {
            @Override
            public void run() {
                GLES20.glUniform2fv(location, 1, FloatBuffer.wrap(arrayValue));
            }
        });
    }

    protected void setFloatVec3(final int location, final float[] arrayValue) {
        runOnDraw(new Runnable() {
            @Override
            public void run() {
                GLES20.glUniform3fv(location, 1, FloatBuffer.wrap(arrayValue));
            }
        });
    }

    protected void setFloatVec4(final int location, final float[] arrayValue) {
        runOnDraw(new Runnable() {
            @Override
            public void run() {
                GLES20.glUniform4fv(location, 1, FloatBuffer.wrap(arrayValue));
            }
        });
    }

    protected void setFloatArray(final int location, final float[] arrayValue) {
        runOnDraw(new Runnable() {
            @Override
            public void run() {
                GLES20.glUniform1fv(location, arrayValue.length, FloatBuffer.wrap(arrayValue));
            }
        });
    }

    protected void setPoint(final int location, final PointF point) {
        runOnDraw(new Runnable() {

            @Override
            public void run() {
                float[] vec2 = new float[2];
                vec2[0] = point.x;
                vec2[1] = point.y;
                GLES20.glUniform2fv(location, 1, vec2, 0);
            }
        });
    }

    protected void setUniformMatrix3f(final int location, final float[] matrix) {
        runOnDraw(new Runnable() {

            @Override
            public void run() {
                GLES20.glUniformMatrix3fv(location, 1, false, matrix, 0);
            }
        });
    }

    protected void setUniformMatrix4f(final int location, final float[] matrix) {
        runOnDraw(new Runnable() {

            @Override
            public void run() {
                GLES20.glUniformMatrix4fv(location, 1, false, matrix, 0);
            }
        });
    }

    protected void runOnDraw(final Runnable runnable) {
        synchronized (mRunOnDraw) {
            mRunOnDraw.addLast(runnable);
        }
    }

    public void onDisplaySizeChanged(final int width, final int height) {
    	mSurfaceWidth = width;
    	mSurfaceHeight = height;
    }
}
