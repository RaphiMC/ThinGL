#version 330 core
#define STYLE_OUTER_BIT 1
#define STYLE_INNER_BIT 2
#define STYLE_ROUNDED_BIT 4

uniform sampler2D u_Source;
uniform sampler2D u_Mask;
uniform bool u_FinalPass;
uniform int u_StyleFlags;
uniform int u_Width;

in vec2 v_VpPixelSize;
in vec2 v_VpTexCoords;
out vec4 o_Color;

int doubleWidth = u_Width * 2;

int decodeDistance(float alpha);
float encodeDistance(int dist);

void main() {
    if (!u_FinalPass) { /* x axis pass */
        vec3 color = vec3(0.0);
        int xDistance = 0;
        vec4 currentPixel = texture(u_Mask, v_VpTexCoords);
        if ((u_StyleFlags & STYLE_OUTER_BIT) != 0 && currentPixel.a == 0.0) {
            for (int i = -u_Width; i <= u_Width; i++) {
                vec4 maskPixel = texture(u_Mask, v_VpTexCoords + vec2(i, 0) * v_VpPixelSize);
                int xDist = abs(i);
                if (maskPixel.a != 0.0 && (xDist < xDistance || xDistance == 0)) {
                    color = maskPixel.rgb;
                    xDistance = xDist;
                }
            }
        }
        if ((u_StyleFlags & STYLE_INNER_BIT) != 0 && currentPixel.a != 0.0) {
            for (int i = -u_Width; i <= u_Width; i++) {
                vec4 maskPixel = texture(u_Mask, v_VpTexCoords + vec2(i, 0) * v_VpPixelSize);
                int xDist = -abs(i);
                if (maskPixel.a == 0.0 && (xDist > xDistance || xDistance == 0)) {
                    color = currentPixel.rgb;
                    xDistance = xDist;
                }
            }
        }

        if (xDistance != 0) {
            o_Color = vec4(color, encodeDistance(xDistance));
        } else {
            vec4 maskPixel = texture(u_Mask, v_VpTexCoords);
            if (maskPixel.a != 0.0) {
                o_Color = vec4(maskPixel.rgb, encodeDistance(0));
            } else {
                discard;
            }
        }
    } else { /* y axis combining pass */
        int xyDistance = 0;
        vec3 color = vec3(0.0);
        vec4 currentPixel = texture(u_Source, v_VpTexCoords);
        if ((u_StyleFlags & STYLE_OUTER_BIT) != 0 && (currentPixel.a == 0.0 || decodeDistance(currentPixel.a) > 0)) {
            for (int i = -u_Width; i <= u_Width; i++) {
                vec4 maskPixel = texture(u_Source, v_VpTexCoords + vec2(0, i) * v_VpPixelSize);
                int dist = abs(i);
                int xDist = decodeDistance(maskPixel.a);
                if (xDist > 0) {
                    dist = int(round(sqrt(float(dist * dist + xDist * xDist))));
                }
                if (maskPixel.a != 0.0 && (dist < xyDistance || xyDistance == 0)) {
                    color = maskPixel.rgb;
                    xyDistance = dist;
                }
            }
        }
        if ((u_StyleFlags & STYLE_INNER_BIT) != 0 && currentPixel.a != 0.0) {
            for (int i = -u_Width; i <= u_Width; i++) {
                vec4 maskPixel = texture(u_Source, v_VpTexCoords + vec2(0, i) * v_VpPixelSize);
                int dist = abs(i);
                int xDist = decodeDistance(maskPixel.a);
                if (xDist < 0) {
                    dist = int(round(sqrt(float(dist * dist + xDist * xDist))));
                    maskPixel.a = 0.0; // Allow the condition below to be true
                }
                dist = -dist;
                if (maskPixel.a == 0.0 && (dist > xyDistance || xyDistance == 0)) {
                    color = currentPixel.rgb;
                    xyDistance = dist;
                }
            }
        }

        if (xyDistance != 0 && ((xyDistance >= -u_Width && xyDistance <= u_Width) || (u_StyleFlags & STYLE_ROUNDED_BIT) == 0)) {
            o_Color = vec4(color, 1.0);
        } else {
            discard;
        }
    }
}

int decodeDistance(float alpha) {
    if (alpha != 0.0) {
        return int(round(alpha * 255.0)) - doubleWidth - 1;
    } else {
        return 0;
    }
}

float encodeDistance(int dist) {
    return float(dist + doubleWidth + 1) / 255.0;
}
