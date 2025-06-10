#version 330 core

uniform mat4 u_ProjectionMatrix;
uniform mat4 u_ViewMatrix;
uniform mat4 u_ModelMatrix;

layout (location = 0) in vec3 i_Position;
layout (location = 1) in vec4 i_Color;
layout (location = 2) in vec3 i_InstancePosition;
layout (location = 3) in vec4 i_InstanceColor;
out vec4 v_Color;

void main() {
    gl_Position = u_ProjectionMatrix * u_ViewMatrix * u_ModelMatrix * vec4(i_Position + i_InstancePosition, 1);

    v_Color = i_Color * i_InstanceColor;
}
