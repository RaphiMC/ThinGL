#version 330 core

uniform sampler2DMS u_Input;
uniform int u_InputSamples;

in vec2 v_VpTexCoord;
out vec4 o_Color;

void main() {
    ivec2 texPos = ivec2(v_VpTexCoord * vec2(textureSize(u_Input)));

    vec4 colorSum = vec4(0.0);
    for (int i = 0; i < u_InputSamples; i++) {
        colorSum += texelFetch(u_Input, texPos, i);
    }

    o_Color = colorSum / float(u_InputSamples);
    if (o_Color.a == 0.0) {
        discard;
    }
}
