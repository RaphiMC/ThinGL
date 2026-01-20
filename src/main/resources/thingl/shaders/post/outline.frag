#version 330 core
#include "../util/easing.glsl"

uniform sampler2D u_Source;
uniform sampler2D u_Input;
uniform int u_Pass;
uniform int u_Width;
uniform int u_StyleFlags;
uniform int u_InterpolationType;

in vec2 v_VpPixelSize;
in vec2 v_VpTexCoord;
out vec4 o_Color;

int decodeDistance(float alpha);
float encodeDistance(int dist);

void main() {
    if (u_Pass == 0) { /* x axis pass */
        vec3 color = vec3(0.0);
        int xDistance = 0;
        vec4 currentPixel = texture(u_Input, v_VpTexCoord);
        if ((u_StyleFlags & STYLE_OUTER_BIT) != 0 && currentPixel.a == 0.0) {
            for (int i = -u_Width; i <= u_Width; i++) {
                vec4 inputPixel = texture(u_Input, v_VpTexCoord + vec2(float(i), 0.0) * v_VpPixelSize);
                int xDist = abs(i);
                if (inputPixel.a != 0.0 && (xDist < xDistance || xDistance == 0)) {
                    color = inputPixel.rgb;
                    xDistance = xDist;
                }
            }
        }
        if ((u_StyleFlags & STYLE_INNER_BIT) != 0 && currentPixel.a != 0.0) {
            for (int i = -u_Width; i <= u_Width; i++) {
                vec4 inputPixel = texture(u_Input, v_VpTexCoord + vec2(float(i), 0.0) * v_VpPixelSize);
                int xDist = -abs(i);
                if (inputPixel.a == 0.0 && (xDist > xDistance || xDistance == 0)) {
                    color = currentPixel.rgb;
                    xDistance = xDist;
                }
            }
        }

        if (xDistance != 0) {
            o_Color = vec4(color, encodeDistance(xDistance));
        } else {
            if (currentPixel.a != 0.0) {
                o_Color = vec4(currentPixel.rgb, encodeDistance(0));
            } else {
                o_Color = vec4(0.0); // AMD Mesa driver workaround
                discard;
            }
        }
    } else { /* y axis combining pass */
        vec3 color = vec3(0.0);
        float xyDistance = 0.0;
        vec4 currentPixel = texture(u_Source, v_VpTexCoord);
        if ((u_StyleFlags & STYLE_OUTER_BIT) != 0 && (currentPixel.a == 0.0 || decodeDistance(currentPixel.a) > 0)) {
            for (int i = -u_Width; i <= u_Width; i++) {
                vec4 inputPixel = texture(u_Source, v_VpTexCoord + vec2(0.0, float(i)) * v_VpPixelSize);
                float xDist = float(decodeDistance(inputPixel.a));
                float yDist = float(abs(i));
                float xyDist = yDist;
                if (xDist > 0.0) {
                    if ((u_StyleFlags & STYLE_SHARP_CORNERS_BIT) == 0) {
                        xyDist = length(vec2(xDist, yDist));
                    } else {
                        xyDist = max(xDist, yDist);
                    }
                }
                if (inputPixel.a != 0.0 && (xyDist < xyDistance || xyDistance == 0.0)) {
                    color = inputPixel.rgb;
                    xyDistance = xyDist;
                }
            }
        }
        if ((u_StyleFlags & STYLE_INNER_BIT) != 0 && currentPixel.a != 0.0) {
            for (int i = -u_Width; i <= u_Width; i++) {
                vec4 inputPixel = texture(u_Source, v_VpTexCoord + vec2(0.0, float(i)) * v_VpPixelSize);
                float xDist = float(decodeDistance(inputPixel.a));
                float yDist = -float(abs(i));
                float xyDist = yDist;
                if (xDist < 0.0) {
                    if ((u_StyleFlags & STYLE_SHARP_CORNERS_BIT) == 0) {
                        xyDist = -length(vec2(xDist, yDist));
                    } else {
                        xyDist = min(xDist, yDist);
                    }
                    inputPixel.a = 0.0; // Allow the condition below to be true
                }
                if (inputPixel.a == 0.0 && (xyDist > xyDistance || xyDistance == 0.0)) {
                    color = currentPixel.rgb;
                    xyDistance = xyDist;
                }
            }
        }

        if (xyDistance != 0.0) {
            float alpha = 0.0;
            if (u_InterpolationType == INTERPOLATION_NONE) {
                alpha = clamp(abs(xyDistance) - float(u_Width), 0.0, 1.0);
            } else {
                alpha = clamp(abs(xyDistance) / float(u_Width), 0.0, 1.0);
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
                o_Color = vec4(color, alpha);
            } else {
                discard;
            }
        } else {
            discard;
        }
    }
}

int decodeDistance(float alpha) {
    if (alpha != 0.0) {
        return int(round(alpha * 255.0)) - (u_Width * 2) - 1;
    } else {
        return 0;
    }
}

float encodeDistance(int dist) {
    return float(dist + (u_Width * 2) + 1) / 255.0;
}
