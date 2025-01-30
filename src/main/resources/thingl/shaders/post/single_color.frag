#version 330 core

uniform sampler2D u_Mask;
uniform vec3 u_Color;

in vec2 v_VpTexCoords;
out vec4 o_Color;

void main() {
    o_Color = vec4(u_Color.rgb, texture(u_Mask, v_VpTexCoords).a);
    if (o_Color.a == 0.0) {
        discard;
    }
}
