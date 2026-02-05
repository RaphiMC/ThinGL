#version 330 core
#include "../util/math.glsl"
#include "../util/easing.glsl"

uniform sampler2D u_Source;
uniform sampler2D u_Input;
uniform int u_Pass;
uniform int u_Width;
uniform uint u_StyleFlags;
uniform uint u_InterpolationType;

in vec2 v_VpPixelSize;
in vec2 v_VpTexCoord;
out vec4 o_Color;

int decodeDistance(float alpha);
float encodeDistance(int dist);

void main() {
    if (u_Pass == 0) { /* x axis pass */
        vec4 currentPixel = texture(u_Input, v_VpTexCoord);
        vec4 nearestPixel = vec4(currentPixel.rgb, 0.0);
        if (bool(u_StyleFlags & STYLE_OUTER_BIT) && currentPixel.a == 0.0) {
            nearestPixel.w = float(u_Width + 1);
            for (int i = -u_Width; i <= u_Width; i++) {
                vec4 inputPixel = texture(u_Input, v_VpTexCoord + vec2(float(i), 0.0) * v_VpPixelSize);
                if (inputPixel.a != 0.0 && float(abs(i)) < nearestPixel.w) {
                    nearestPixel = vec4(inputPixel.rgb, float(abs(i)));
                }
            }
        }
        if (bool(u_StyleFlags & STYLE_INNER_BIT) && currentPixel.a != 0.0) {
            nearestPixel.w = -float(u_Width + 1);
            for (int i = -u_Width; i <= u_Width; i++) {
                vec4 inputPixel = texture(u_Input, v_VpTexCoord + vec2(float(i), 0.0) * v_VpPixelSize);
                if (inputPixel.a == 0.0 && float(-abs(i)) > nearestPixel.w) {
                    nearestPixel = vec4(currentPixel.rgb, float(-abs(i)));
                }
            }
        }
        o_Color = vec4(nearestPixel.rgb, encodeDistance(int(nearestPixel.w)));
    } else { /* y axis combining pass */
        vec4 currentPixel = texture(u_Source, v_VpTexCoord);
        vec4 nearestPixel = vec4(vec3(0.0), 0.0);
        if (bool(u_StyleFlags & STYLE_OUTER_BIT) && decodeDistance(currentPixel.a) > 0) {
            nearestPixel.w = float(u_Width + 1);
            for (int i = -u_Width; i <= u_Width; i++) {
                vec4 inputPixel = texture(u_Source, v_VpTexCoord + vec2(0.0, float(i)) * v_VpPixelSize);
                int xDist = decodeDistance(inputPixel.a);
                int yDist = abs(i);
                float xyDist;
                if (xDist <= 0) {
                    xyDist = float(yDist);
                } else if (!bool(u_StyleFlags & STYLE_SHARP_CORNERS_BIT)) {
                    xyDist = length(vec2(float(xDist), float(yDist)));
                } else {
                    xyDist = float(max(xDist, yDist));
                }
                if (xyDist < nearestPixel.w) {
                    nearestPixel = vec4(inputPixel.rgb, xyDist);
                }
            }
        }
        if (bool(u_StyleFlags & STYLE_INNER_BIT) && decodeDistance(currentPixel.a) < 0) {
            nearestPixel.w = -float(u_Width + 1);
            for (int i = -u_Width; i <= u_Width; i++) {
                vec4 inputPixel = texture(u_Source, v_VpTexCoord + vec2(0.0, float(i)) * v_VpPixelSize);
                int xDist = decodeDistance(inputPixel.a);
                int yDist = -abs(i);
                float xyDist;
                if (xDist >= 0) {
                    xyDist = float(yDist);
                } else if (!bool(u_StyleFlags & STYLE_SHARP_CORNERS_BIT)) {
                    xyDist = -length(vec2(float(xDist), float(yDist)));
                } else {
                    xyDist = float(min(xDist, yDist));
                }
                if (xyDist > nearestPixel.w) {
                    nearestPixel = vec4(currentPixel.rgb, xyDist);
                }
            }
        }
        nearestPixel.w = abs(nearestPixel.w);

        if (nearestPixel.w > 0.0 && nearestPixel.w <= float(u_Width + 1)) {
            float alpha;
            if (u_InterpolationType == INTERPOLATION_NONE) {
                alpha = clamp(nearestPixel.w - float(u_Width), 0.0, 1.0);
            } else {
                alpha = clamp(nearestPixel.w / float(u_Width), 0.0, 1.0);
                switch (u_InterpolationType) {
                    case INTERPOLATION_EASE_IN_SINE: alpha = easeInSine(alpha); break;
                    case INTERPOLATION_EASE_OUT_SINE: alpha = easeOutSine(alpha); break;
                    case INTERPOLATION_EASE_IN_OUT_SINE: alpha = easeInOutSine(alpha); break;
                    case INTERPOLATION_EASE_IN_QUAD: alpha = easeInQuad(alpha); break;
                    case INTERPOLATION_EASE_OUT_QUAD: alpha = easeOutQuad(alpha); break;
                    case INTERPOLATION_EASE_IN_OUT_QUAD: alpha = easeInOutQuad(alpha); break;
                    case INTERPOLATION_EASE_IN_CUBIC: alpha = easeInCubic(alpha); break;
                    case INTERPOLATION_EASE_OUT_CUBIC: alpha = easeOutCubic(alpha); break;
                    case INTERPOLATION_EASE_IN_OUT_CUBIC: alpha = easeInOutCubic(alpha); break;
                    case INTERPOLATION_EASE_IN_QUART: alpha = easeInQuart(alpha); break;
                    case INTERPOLATION_EASE_OUT_QUART: alpha = easeOutQuart(alpha); break;
                    case INTERPOLATION_EASE_IN_OUT_QUART: alpha = easeInOutQuart(alpha); break;
                    case INTERPOLATION_EASE_IN_QUINT: alpha = easeInQuint(alpha); break;
                    case INTERPOLATION_EASE_OUT_QUINT: alpha = easeOutQuint(alpha); break;
                    case INTERPOLATION_EASE_IN_OUT_QUINT: alpha = easeInOutQuint(alpha); break;
                    case INTERPOLATION_EASE_IN_EXPO: alpha = easeInExpo(alpha); break;
                    case INTERPOLATION_EASE_OUT_EXPO: alpha = easeOutExpo(alpha); break;
                    case INTERPOLATION_EASE_IN_OUT_EXPO: alpha = easeInOutExpo(alpha); break;
                    case INTERPOLATION_EASE_IN_CIRC: alpha = easeInCirc(alpha); break;
                    case INTERPOLATION_EASE_OUT_CIRC: alpha = easeOutCirc(alpha); break;
                    case INTERPOLATION_EASE_IN_OUT_CIRC: alpha = easeInOutCirc(alpha); break;
                }
            }
            alpha = clamp(1.0 - alpha, 0.0, 1.0);
            if (alpha != 0.0) {
                o_Color = vec4(nearestPixel.rgb, alpha);
            } else {
                discard;
            }
        } else {
            discard;
        }
    }
}

int decodeDistance(float alpha) {
    return int(round(map(alpha, 0.0, 1.0, -float(u_Width + 1), float(u_Width + 1))));
}

float encodeDistance(int dist) {
    return map(float(dist), -float(u_Width + 1), float(u_Width + 1), 0.0, 1.0);
}
