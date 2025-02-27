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

vec4 getPixel(vec2 pos);
bool shouldBlur(vec2 pos);

void main() {
    if (shouldBlur(v_VpTexCoords)) {
        float o = u_Offset;
        if (u_Pass < 2) { /* Downsample */
            vec4 col = getPixel(v_VpTexCoords) * 4.0;
            col += getPixel(v_VpTexCoords + vec2(-o, -o) * v_VpPixelSize);
            col += getPixel(v_VpTexCoords + vec2(o, o) * v_VpPixelSize);
            col += getPixel(v_VpTexCoords + vec2(o, -o) * v_VpPixelSize);
            col += getPixel(v_VpTexCoords + vec2(-o, o) * v_VpPixelSize);
            o_Color = col / col.a;
        } else { /* Upsample */
            vec4 col = getPixel(v_VpTexCoords + vec2(-o, 0.0) * v_VpPixelSize);
            col += getPixel(v_VpTexCoords + vec2(-o, o) * v_VpPixelSize) * 2.0;
            col += getPixel(v_VpTexCoords + vec2(0.0, o) * v_VpPixelSize);
            col += getPixel(v_VpTexCoords + vec2(o, o) * v_VpPixelSize) * 2.0;
            col += getPixel(v_VpTexCoords + vec2(o, 0.0) * v_VpPixelSize);
            col += getPixel(v_VpTexCoords + vec2(o, -o) * v_VpPixelSize) * 2.0;
            col += getPixel(v_VpTexCoords + vec2(0.0, -o) * v_VpPixelSize);
            col += getPixel(v_VpTexCoords + vec2(-o, -o) * v_VpPixelSize) * 2.0;
            o_Color = col / col.a;
        }
    } else {
        discard;
    }
}

vec4 getPixel(vec2 pos) {
    if (shouldBlur(pos)) {
        vec4 color = texture(u_Source, pos * multiplier);
        if (color.a != 0.0) {
            return vec4(color.rgb, 1.0);
        }
    }
    return vec4(0.0);
}

bool shouldBlur(vec2 pos) {
    return u_Pass != 3 || texture(u_Mask, pos).a != 0.0;
}
