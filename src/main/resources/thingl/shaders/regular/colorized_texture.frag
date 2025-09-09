#version 330 core
#define LUMINOSITY_WEIGHTS vec3(0.2126, 0.7152, 0.0722)

uniform vec4 u_ColorModifier;
uniform sampler2D u_Texture;

in vec4 v_Color;
in vec2 v_TexCoord;
out vec4 o_Color;

void main() {
    vec4 textureColor = texture(u_Texture, v_TexCoord);
    vec3 grayScale = vec3(dot(textureColor.rgb, LUMINOSITY_WEIGHTS));
    float colorAverage = (v_Color.r + v_Color.g + v_Color.b) / 3;
    vec3 colorPow = vec3(1 + colorAverage) - v_Color.rgb;
    o_Color = vec4(pow(grayScale, colorPow), textureColor.a * v_Color.a) * u_ColorModifier;
    if (o_Color.a == 0) {
        discard;
    }
}
