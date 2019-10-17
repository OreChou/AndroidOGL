#extension GL_OES_EGL_image_external : require
varying highp vec2 vTexCoord;
uniform samplerExternalOES sTexture;
uniform sampler2D iTexture;
uniform highp mat4 uSTMatrix;
uniform highp float i;
void main() {
    highp vec2 tx_transformed = (uSTMatrix * vec4(vTexCoord, 0, 1.0)).xy;
    highp vec4 video = texture2D(sTexture, tx_transformed);
    highp vec4 rgba;
    if(i == 0.0){
        rgba = video;
    }
    else{
        highp vec4 image = texture2D(iTexture, vTexCoord);
        rgba = mix(video,image,image.a);
    }
    gl_FragColor = rgba;
}