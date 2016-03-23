package com.taro.videoapp.shadercamera.camera.preview;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;

import android.opengl.GLES11Ext;
import android.opengl.GLES20;
import android.opengl.Matrix;
import android.util.Log;

public class DirectDrawer {
	private final String vertexShaderCode =
            "attribute vec4 vPosition;" +
            "attribute vec2 inputTextureCoordinate;" +
            "varying highp vec2 vTexCoord;" +
            "void main()" +
            "{"+
                "gl_Position = vPosition;"+
                "vTexCoord = inputTextureCoordinate;" +
            "}";

    private final String fragmentShaderCode =
            "#extension GL_OES_EGL_image_external : require\n"+
            "precision mediump float;\n" +
                    "\n" +
                    "varying highp vec2 vTexCoord;\n" +
                    "uniform samplerExternalOES sTexture;\n" +
                    "\n" +
                    "\n" +
                    "vec3 rgb2hsv(vec3 c)\n" +
                    "{\n" +
                    "    vec4 K = vec4(0.0, -1.0 / 3.0, 2.0 / 3.0, -1.0);\n" +
                    "    vec4 p = mix(vec4(c.bg, K.wz), vec4(c.gb, K.xy), step(c.b, c.g));\n" +
                    "    vec4 q = mix(vec4(p.xyw, c.r), vec4(c.r, p.yzx), step(p.x, c.r));\n" +
                    "    \n" +
                    "    float d = q.x - min(q.w, q.y);\n" +
                    "    float e = 1.0e-10;\n" +
                    "    return vec3(abs(q.z + (q.w - q.y) / (6.0 * d + e)), d / (q.x + e), q.x);\n" +
                    "}\n" +
                    "\n" +
                    "vec3 hsv2rgb(vec3 c)\n" +
                    "{\n" +
                    "    vec4 K = vec4(1.0, 2.0 / 3.0, 1.0 / 3.0, 3.0);\n" +
                    "    vec3 p = abs(fract(c.xxx + K.xyz) * 6.0 - K.www);\n" +
                    "    return c.z * mix(K.xxx, clamp(p - K.xxx, 0.0, 1.0), c.y);\n" +
                    "}\n" +
                    "\n" +
                    "float h(float x) {\n" +
                    "    x = x * 360.;\n" +
                    "    float r;\n" +
                    "    if (x < 180.) {\n" +
                    "        r =  -.0018937600605962723*x*x+.4545024145438461*x+39.54739134551675;\n" +
                    "    } else {\n" +
                    "        r =  .2222222222222222*x+160.0;\n" +
                    "    }\n" +
                    "    return r / 360.;\n" +
                    "}\n" +
                    "\n" +
                    "float sc(float x) {\n" +
                    "    float xx = 2. * (x - .5);\n" +
                    "    float r = pow(abs(xx),.56);\n" +
                    "    //  r = 1.;\n" +
                    "    return r;\n" +
                    "}\n" +
                    "\n" +
                    "float vc(float x) {\n" +
                    "    float xx = 2. * (x - .5);\n" +
                    "    float r = 1. - xx * xx * xx * xx;\n" +
                    "    return r;\n" +
                    "}\n" +
                    "\n" +
                    "\n" +
                    "void main()\n" +
                    "{\n" +
                    "    mediump vec3 _gb = texture2D(sTexture, vTexCoord).rgb;\n" +
                    "    _gb.x = .0;\n" +
                    "    _gb = rgb2hsv(_gb);\n" +
                    "    \n" +
                    "    float x = _gb.x;\n" +
                    "    _gb.x = h(x);\n" +
                    "    _gb.y = sc(x) * _gb.y;\n" +
                    "    _gb.z = _gb.z;\n" +
                    "    _gb = hsv2rgb(_gb);\n" +
                    "    gl_FragColor = vec4(_gb, 1);\n" +
                    "}";

    private FloatBuffer vertexBuffer, textureVerticesBuffer;
    private ShortBuffer drawListBuffer;
    private final int mProgram;
    private int mPositionHandle;
    private int mTextureCoordHandle;

    private short drawOrder[] = { 0, 1, 2, 0, 2, 3 }; // order to draw vertices

    // number of coordinates per vertex in this array
    private static final int COORDS_PER_VERTEX = 2;

    private final int vertexStride = COORDS_PER_VERTEX * 4; // 4 bytes per vertex

    static float squareCoords[] = {
       -1.0f,  1.0f,
       -1.0f, -1.0f,
        1.0f, -1.0f,
        1.0f,  1.0f,
    };

    static float textureVertices[] = {
        0.0f, 1.0f,
        1.0f, 1.0f,
        1.0f, 0.0f,
        0.0f, 0.0f,
    };

    private int texture;

    public DirectDrawer(int texture)
    {
        this.texture = texture;
        // initialize vertex byte buffer for shape coordinates
        ByteBuffer bb = ByteBuffer.allocateDirect(squareCoords.length * 4);
        bb.order(ByteOrder.nativeOrder());
        vertexBuffer = bb.asFloatBuffer();
        vertexBuffer.put(squareCoords);
        vertexBuffer.position(0);

        // initialize byte buffer for the draw list
        ByteBuffer dlb = ByteBuffer.allocateDirect(drawOrder.length * 2);
        dlb.order(ByteOrder.nativeOrder());
        drawListBuffer = dlb.asShortBuffer();
        drawListBuffer.put(drawOrder);
        drawListBuffer.position(0);

        ByteBuffer bb2 = ByteBuffer.allocateDirect(textureVertices.length * 4);
        bb2.order(ByteOrder.nativeOrder());
        textureVerticesBuffer = bb2.asFloatBuffer();
        textureVerticesBuffer.put(textureVertices);
        textureVerticesBuffer.position(0);

        int vertexShader    = loadShader(GLES20.GL_VERTEX_SHADER, vertexShaderCode);
        int fragmentShader  = loadShader(GLES20.GL_FRAGMENT_SHADER, fragmentShaderCode);

        mProgram = GLES20.glCreateProgram();             // create empty OpenGL ES Program
        GLES20.glAttachShader(mProgram, vertexShader);   // add the vertex shader to program
        GLES20.glAttachShader(mProgram, fragmentShader); // add the fragment shader to program
        GLES20.glLinkProgram(mProgram);                  // creates OpenGL ES program executables
    }

    public void draw(float[] mtx)
    {
        GLES20.glUseProgram(mProgram);

        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
        GLES20.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, texture);

        // get handle to vertex shader's vPosition member
        mPositionHandle = GLES20.glGetAttribLocation(mProgram, "vPosition");
        // Enable a handle to the triangle vertices
        GLES20.glEnableVertexAttribArray(mPositionHandle);
        checkGlError("glEnableVertexAttribArray vPosition");

        // Prepare the <insert shape here> coordinate data
        GLES20.glVertexAttribPointer(mPositionHandle, COORDS_PER_VERTEX, GLES20.GL_FLOAT, false, vertexStride, vertexBuffer);
        checkGlError("glVertexAttribPointer vPosition");

        mTextureCoordHandle = GLES20.glGetAttribLocation(mProgram, "inputTextureCoordinate");
        GLES20.glEnableVertexAttribArray(mTextureCoordHandle);
        checkGlError("glEnableVertexAttribArray inputTextureCoordinate");
//        textureVerticesBuffer.clear();
//        textureVerticesBuffer.put( transformTextureCoordinates( textureVertices, mtx ));
//        textureVerticesBuffer.position(0);
        
        GLES20.glVertexAttribPointer(mTextureCoordHandle, COORDS_PER_VERTEX, GLES20.GL_FLOAT, false, vertexStride, textureVerticesBuffer);
        checkGlError("glVertexAttribPointer inputTextureCoordinate");

        GLES20.glDrawElements(GLES20.GL_TRIANGLES, drawOrder.length, GLES20.GL_UNSIGNED_SHORT, drawListBuffer);

        // Disable vertex array
        GLES20.glDisableVertexAttribArray(mPositionHandle);
        GLES20.glDisableVertexAttribArray(mTextureCoordHandle);
    }
    
    private  int loadShader(int type, String shaderCode){

        // create a vertex shader type (GLES20.GL_VERTEX_SHADER)
        // or a fragment shader type (GLES20.GL_FRAGMENT_SHADER)
        int shader = GLES20.glCreateShader(type);

        // add the source code to the shader and compile it
        GLES20.glShaderSource(shader, shaderCode);
        GLES20.glCompileShader(shader);

        return shader;
    }
    private float[] transformTextureCoordinates( float[] coords, float[] matrix)
    {          
       float[] result = new float[ coords.length ];        
       float[] vt = new float[4];      

       for ( int i = 0 ; i < coords.length ; i += 2 ) {
           float[] v = { coords[i], coords[i+1], 0 , 1  };
           Matrix.multiplyMV(vt, 0, matrix, 0, v, 0);
           result[i] = vt[0];
           result[i+1] = vt[1];
       }
       return result;
    }

    /**
     * Checks to see if a GLES error has been raised.
     */
    public static void checkGlError(String op) {
        int error = GLES20.glGetError();
        if (error != GLES20.GL_NO_ERROR) {
            String msg = op + ": glError 0x" + Integer.toHexString(error);
            Log.e("checkGlError", msg);
            throw new RuntimeException(msg);
        }
    }

}
