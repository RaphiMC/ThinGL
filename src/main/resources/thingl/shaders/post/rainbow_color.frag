#version 330 core
#include "../util/hsv_rgb.glsl"

uniform sampler2D u_Input;
uniform float u_Time;
uniform float u_SpeedDivider;
uniform float u_RainbowDivider;
uniform float u_Offset;
uniform vec2 u_Direction;

in vec2 v_RelTexCoord;
in vec2 v_VpTexCoord;
out vec4 o_Color;

void main() {
    vec4 inputPixel = texture(u_Input, v_VpTexCoord);
    if (inputPixel.a != 0.0) {
        vec2 position = v_RelTexCoord / u_RainbowDivider * u_Direction;
        vec3 hsv = rgb2hsv(inputPixel.rgb);
        hsv.x = position.x + position.y + u_Offset - u_Time / u_SpeedDivider;
        o_Color = vec4(hsv2rgb(hsv), inputPixel.a);
    } else {
        discard;
    }
}
