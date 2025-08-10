// https://www.shadertoy.com/view/3td3W8
#version 330 core

uniform sampler2D u_Source;
uniform sampler2D u_Input;
uniform int u_Pass;
uniform float u_Offset;

in vec2 v_VpPixelSize;
in vec2 v_VpTexCoord;
out vec4 o_Color;

vec4 getPixel(vec2 pos);
bool shouldBlur(vec2 pos);

void main() {
    if (shouldBlur(v_VpTexCoord)) {
        float o = u_Offset;
        if (u_Pass < 2) { /* Downsample */
            vec4 col = getPixel(v_VpTexCoord) * 4;
            col += getPixel(v_VpTexCoord + vec2(-o, -o) * v_VpPixelSize);
            col += getPixel(v_VpTexCoord + vec2(o, o) * v_VpPixelSize);
            col += getPixel(v_VpTexCoord + vec2(o, -o) * v_VpPixelSize);
            col += getPixel(v_VpTexCoord + vec2(-o, o) * v_VpPixelSize);
            o_Color = col / col.a;
        } else { /* Upsample */
            vec4 col = getPixel(v_VpTexCoord + vec2(-o, 0) * v_VpPixelSize);
            col += getPixel(v_VpTexCoord + vec2(-o, o) * v_VpPixelSize) * 2;
            col += getPixel(v_VpTexCoord + vec2(0, o) * v_VpPixelSize);
            col += getPixel(v_VpTexCoord + vec2(o, o) * v_VpPixelSize) * 2;
            col += getPixel(v_VpTexCoord + vec2(o, 0) * v_VpPixelSize);
            col += getPixel(v_VpTexCoord + vec2(o, -o) * v_VpPixelSize) * 2;
            col += getPixel(v_VpTexCoord + vec2(0, -o) * v_VpPixelSize);
            col += getPixel(v_VpTexCoord + vec2(-o, -o) * v_VpPixelSize) * 2;
            o_Color = col / col.a;
        }
    } else {
        discard;
    }
}

vec4 getPixel(vec2 pos) {
    if (shouldBlur(pos)) {
        return vec4(texture(u_Source, pos).rgb, 1);
    } else {
        return vec4(0);
    }
}

bool shouldBlur(vec2 pos) {
    return u_Pass != 3 || texture(u_Input, pos).a != 0;
}
