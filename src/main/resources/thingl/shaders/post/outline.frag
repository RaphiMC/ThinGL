#version 330 core
#define STYLE_OUTER_BIT 1
#define STYLE_INNER_BIT 2
#define STYLE_SHARP_CORNERS_BIT 4

uniform sampler2D u_Source;
uniform sampler2D u_Input;
uniform int u_Pass;
uniform int u_Width;
uniform int u_StyleFlags;

in vec2 v_VpPixelSize;
in vec2 v_VpTexCoord;
out vec4 o_Color;

int doubleWidth = u_Width * 2;

int decodeDistance(float alpha);
float encodeDistance(int dist);

void main() {
    if (u_Pass == 0) { /* x axis pass */
        vec3 color = vec3(0);
        int xDistance = 0;
        vec4 currentPixel = texture(u_Input, v_VpTexCoord);
        if ((u_StyleFlags & STYLE_OUTER_BIT) != 0 && currentPixel.a == 0) {
            for (int i = -u_Width; i <= u_Width; i++) {
                vec4 inputPixel = texture(u_Input, v_VpTexCoord + vec2(i, 0) * v_VpPixelSize);
                int xDist = abs(i);
                if (inputPixel.a != 0 && (xDist < xDistance || xDistance == 0)) {
                    color = inputPixel.rgb;
                    xDistance = xDist;
                }
            }
        }
        if ((u_StyleFlags & STYLE_INNER_BIT) != 0 && currentPixel.a != 0) {
            for (int i = -u_Width; i <= u_Width; i++) {
                vec4 inputPixel = texture(u_Input, v_VpTexCoord + vec2(i, 0) * v_VpPixelSize);
                int xDist = -abs(i);
                if (inputPixel.a == 0 && (xDist > xDistance || xDistance == 0)) {
                    color = currentPixel.rgb;
                    xDistance = xDist;
                }
            }
        }

        if (xDistance != 0) {
            o_Color = vec4(color, encodeDistance(xDistance));
        } else {
            vec4 inputPixel = texture(u_Input, v_VpTexCoord);
            if (inputPixel.a != 0) {
                o_Color = vec4(inputPixel.rgb, encodeDistance(0));
            } else {
                discard;
            }
        }
    } else { /* y axis combining pass */
        vec3 color = vec3(0);
        float xyDistance = 0;
        vec4 currentPixel = texture(u_Source, v_VpTexCoord);
        if ((u_StyleFlags & STYLE_OUTER_BIT) != 0 && (currentPixel.a == 0 || decodeDistance(currentPixel.a) > 0)) {
            for (int i = -u_Width; i <= u_Width; i++) {
                vec4 inputPixel = texture(u_Source, v_VpTexCoord + vec2(0, i) * v_VpPixelSize);
                int xDist = decodeDistance(inputPixel.a);
                int yDist = abs(i);
                float xyDist = yDist;
                if (xDist > 0) {
                    xyDist = sqrt(xDist * xDist + yDist * yDist);
                }
                if (inputPixel.a != 0 && (xyDist < xyDistance || xyDistance == 0)) {
                    color = inputPixel.rgb;
                    xyDistance = xyDist;
                }
            }
        }
        if ((u_StyleFlags & STYLE_INNER_BIT) != 0 && currentPixel.a != 0) {
            for (int i = -u_Width; i <= u_Width; i++) {
                vec4 inputPixel = texture(u_Source, v_VpTexCoord + vec2(0, i) * v_VpPixelSize);
                int xDist = decodeDistance(inputPixel.a);
                int yDist = -abs(i);
                float xyDist = yDist;
                if (xDist < 0) {
                    xyDist = -sqrt(xDist * xDist + yDist * yDist);
                    inputPixel.a = 0; // Allow the condition below to be true
                }
                if (inputPixel.a == 0 && (xyDist > xyDistance || xyDistance == 0)) {
                    color = currentPixel.rgb;
                    xyDistance = xyDist;
                }
            }
        }

        if (xyDistance != 0) {
            if ((u_StyleFlags & STYLE_SHARP_CORNERS_BIT) == 0) {
                float alpha = clamp(1 - (abs(xyDistance) - u_Width), 0, 1);
                if (alpha > 0) {
                    o_Color = vec4(color, alpha);
                } else {
                    discard;
                }
            } else {
                o_Color = vec4(color, 1);
            }
        } else {
            discard;
        }
    }
}

int decodeDistance(float alpha) {
    if (alpha != 0) {
        return int(round(alpha * 255)) - doubleWidth - 1;
    } else {
        return 0;
    }
}

float encodeDistance(int dist) {
    return (dist + doubleWidth + 1) / 255.0;
}
