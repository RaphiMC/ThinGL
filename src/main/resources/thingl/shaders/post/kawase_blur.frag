// https://www.shadertoy.com/view/3td3W8
#version 330 core

uniform vec2 u_Viewport;
uniform sampler2D u_Source;
uniform sampler2D u_Mask;
uniform int u_Pass;
uniform float u_Offset;

in vec2 v_VpPixelSize;
in vec2 v_VpTexCoords;
out vec4 o_Color;

float multiplier = u_Pass < 2 ? 2.0 : 0.5;

vec4 downsample(vec2 pos);
vec4 upsample(vec2 pos);
vec4 getPixel(vec2 pos);
bool shouldBlur(vec2 pos);

void main() {
    if (shouldBlur(v_VpTexCoords)) {
        if (u_Pass < 2) {
            o_Color = downsample(v_VpTexCoords);
        } else {
            o_Color = upsample(v_VpTexCoords);
        }
    } else {
        discard;
    }
}

vec4 downsample(vec2 pos) {
    float o = u_Offset;

    vec4 col = getPixel(pos) * 4.0;
    col += getPixel(pos + vec2(-o, -o) * v_VpPixelSize);
    col += getPixel(pos + vec2(o, o) * v_VpPixelSize);
    col += getPixel(pos + vec2(o, -o) * v_VpPixelSize);
    col += getPixel(pos + vec2(-o, o) * v_VpPixelSize);

    return col / col.a;
}

vec4 upsample(vec2 pos) {
    float o = u_Offset;

    vec4 col = getPixel(pos + vec2(-o, 0.0) * v_VpPixelSize);
    col += getPixel(pos + vec2(-o, o) * v_VpPixelSize) * 2.0;
    col += getPixel(pos + vec2(0.0, o) * v_VpPixelSize);
    col += getPixel(pos + vec2(o, o) * v_VpPixelSize) * 2.0;
    col += getPixel(pos + vec2(o, 0.0) * v_VpPixelSize);
    col += getPixel(pos + vec2(o, -o) * v_VpPixelSize) * 2.0;
    col += getPixel(pos + vec2(0.0, -o) * v_VpPixelSize);
    col += getPixel(pos + vec2(-o, -o) * v_VpPixelSize) * 2.0;

    return col / col.a;
}

vec4 getPixel(vec2 pos) {
    if (shouldBlur(pos)) {
        vec4 color = texture(u_Source, pos * multiplier);
        if (color.a > 0.0) {
            return vec4(color.rgb, 1.0);
        }
    }
    return vec4(0.0);
}

bool shouldBlur(vec2 pos) {
    return u_Pass != 3 || texture(u_Mask, pos).a != 0.0;
}
