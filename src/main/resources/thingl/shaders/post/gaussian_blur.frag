#version 330 core
#define M_PI 3.14159265359

uniform sampler2D u_Source;
uniform sampler2D u_Mask;
uniform bool u_FinalPass;
uniform int u_Radius;
uniform float u_Sigma;

in vec2 v_VpPixelSize;
in vec2 v_VpTexCoords;
out vec4 o_Color;

float normalization = 1.0 / (u_Sigma * sqrt(2.0 * M_PI));
float sigmaSquared = u_Sigma * u_Sigma;

vec4 getPixel(vec2 pos);
bool shouldBlur(vec2 pos);
float gaussian(float x);

void main() {
    if (shouldBlur(v_VpTexCoords)) {
        vec4 colorSum = vec4(0.0);
        if (!u_FinalPass) { /* x axis pass */
            for (int i = -u_Radius; i <= u_Radius; i++) {
                colorSum += getPixel(v_VpTexCoords + vec2(i, 0) * v_VpPixelSize) * gaussian(float(i));
            }
        } else { /* y axis pass */
            for (int i = -u_Radius; i <= u_Radius; i++) {
                colorSum += getPixel(v_VpTexCoords + vec2(0, i) * v_VpPixelSize) * gaussian(float(i));
            }
        }
        o_Color = colorSum / colorSum.a;
    } else {
        discard;
    }
}

vec4 getPixel(vec2 pos) {
    if (shouldBlur(pos)) {
        return vec4(texture(u_Source, pos).rgb, 1.0);
    } else {
        return vec4(0.0);
    }
}

bool shouldBlur(vec2 pos) {
    return texture(u_Mask, pos).a != 0.0;
}

float gaussian(float x) {
    float exponent = exp(-(x * x) / (2.0 * sigmaSquared));
    return normalization * exponent;
}
