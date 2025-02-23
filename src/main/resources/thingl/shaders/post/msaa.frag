#version 450 core

uniform sampler2DMS u_Mask;

in vec2 v_VpTexCoords;
out vec4 o_Color;

void main() {
    ivec2 texPos = ivec2(v_VpTexCoords * textureSize(u_Mask));
    int samples = textureSamples(u_Mask);

    vec4 colorSum = vec4(0.0);
    for (int i = 0; i < samples; i++) {
        colorSum += texelFetch(u_Mask, texPos, i);
    }

    o_Color = colorSum / float(samples);
    if (o_Color.a == 0.0) {
        discard;
    }
}
