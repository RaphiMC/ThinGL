#version 330 core

uniform sampler2D u_Mask;
uniform vec4 u_Color;

in vec2 v_VpTexCoord;
out vec4 o_Color;

void main() {
    o_Color = vec4(u_Color.rgb, texture(u_Mask, v_VpTexCoord).a * u_Color.a);
    if (o_Color.a == 0) {
        discard;
    }
}
