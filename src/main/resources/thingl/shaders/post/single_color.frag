#version 330 core

uniform sampler2D u_Input;
uniform vec4 u_Color;
uniform bool u_UseInputAlpha;

in vec2 v_VpTexCoord;
out vec4 o_Color;

void main() {
    o_Color = texture(u_Input, v_VpTexCoord);
    if (u_UseInputAlpha) {
        o_Color = vec4(u_Color.rgb, o_Color.a * u_Color.a);
    } else if (o_Color.a != 0.0) {
        o_Color = u_Color;
    }
    if (o_Color.a == 0.0) {
        discard;
    }
}
