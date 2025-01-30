// https://www.shadertoy.com/view/3ltfzl
#version 330 core

uniform sampler2D u_Source;
uniform sampler2D u_Mask;
uniform bool u_FinalPass;
uniform float u_Radius;
uniform float u_Sigma;

in vec2 v_VpPixelSize;
in vec2 v_VpTexCoords;
out vec4 o_Color;

float m = 0.398942280401 /* 1 / sqrt(2 * PI) */ / u_Sigma;
float sigma2 = u_Sigma * u_Sigma;

vec4 blur(vec2 pos);
float gaussian(float x);
vec4 getPixel(vec2 pos);
bool shouldBlur(vec2 pos);

void main() {
    if (shouldBlur(v_VpTexCoords)) {
        o_Color = blur(v_VpTexCoords);
    } else {
        discard;
    }
}

vec4 blur(vec2 pos) {
    vec4 col = vec4(0.0);
    for (float i = -u_Radius; i <= u_Radius; i++) {
        vec2 coords = pos + (u_FinalPass ? vec2(0, i) : vec2(i, 0)) * v_VpPixelSize;
        col += getPixel(coords) * gaussian(i);
    }

    return col / col.a;
}

float gaussian(float x) {
    return m * exp(-x * x / 2 / sigma2);
}

vec4 getPixel(vec2 pos) {
    if (shouldBlur(pos)) {
        vec4 color = texture(u_Source, pos);
        if (color.a > 0.0) {
            return vec4(color.rgb, 1.0);
        }
    }
    return vec4(0.0);
}

bool shouldBlur(vec2 pos) {
    return texture(u_Mask, pos).a != 0.0;
}
