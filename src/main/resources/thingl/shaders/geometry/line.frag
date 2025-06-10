#version 330 core

uniform vec4 u_ColorModifier;

in vec4 g_Color;
out vec4 o_Color;

void main() {
    o_Color = g_Color * u_ColorModifier;
    if (o_Color.a == 0) {
        discard;
    }
}
