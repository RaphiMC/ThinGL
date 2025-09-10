#version 430 core
#extension GL_ARB_shader_draw_parameters: require

uniform mat4 u_ProjectionMatrix;
uniform mat4 u_ViewMatrix;
uniform mat4 u_ModelMatrix;

layout (std430) restrict readonly buffer ssbo_DrawData {
    vec3 positionOffsets[];
};

layout (location = 0) in vec3 a_Position;
layout (location = 1) in vec4 a_Color;
out vec4 v_Color;

void main() {
    if (positionOffsets.length() == 0) {
        gl_Position = u_ProjectionMatrix * u_ViewMatrix * u_ModelMatrix * vec4(a_Position, 1);
    } else {
        gl_Position = u_ProjectionMatrix * u_ViewMatrix * u_ModelMatrix * vec4(a_Position + positionOffsets[gl_DrawIDARB], 1);
    }

    v_Color = a_Color;
}
