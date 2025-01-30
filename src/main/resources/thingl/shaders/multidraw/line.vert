#version 430 core
#extension GL_ARB_shader_draw_parameters: require

uniform mat4 u_ProjectionMatrix;
uniform mat4 u_ViewMatrix;
uniform mat4 u_ModelMatrix;

layout (std430) readonly buffer ssbo_DrawData {
    vec3 positionOffsets[];
};

layout (location = 0) in vec3 i_Position;
layout (location = 1) in vec4 i_Color;
layout (location = 2) in float i_LineWidth;
out vec4 v_Color;
out float v_LineWidth;

void main() {
    if (positionOffsets.length() == 0) {
        gl_Position = u_ProjectionMatrix * u_ViewMatrix * u_ModelMatrix * vec4(i_Position, 1.0);
    } else {
        gl_Position = u_ProjectionMatrix * u_ViewMatrix * u_ModelMatrix * vec4(i_Position + positionOffsets[gl_DrawIDARB], 1.0);
    }

    v_Color = i_Color;
    v_LineWidth = i_LineWidth;
}
