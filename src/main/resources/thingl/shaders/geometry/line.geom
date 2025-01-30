#version 330 core

uniform vec2 u_Viewport;

layout (lines) in;
layout (triangle_strip, max_vertices = 4) out;

in vec4 v_Color[];
in float v_LineWidth[];
out vec4 g_Color;

void main() {
    vec4 p1 = gl_in[0].gl_Position;
    vec4 p2 = gl_in[1].gl_Position;
    vec2 dir = normalize((p2.xy / p2.w - p1.xy / p1.w) * u_Viewport);

    vec2 offset1 = vec2(-dir.y, dir.x) * v_LineWidth[0] / u_Viewport;
    g_Color = v_Color[0];
    gl_Position = p1 + vec4(offset1.xy * p1.w, 0.0, 0.0);
    EmitVertex();
    gl_Position = p1 - vec4(offset1.xy * p1.w, 0.0, 0.0);
    EmitVertex();

    vec2 offset2 = vec2(-dir.y, dir.x) * v_LineWidth[1] / u_Viewport;
    g_Color = v_Color[1];
    gl_Position = p2 + vec4(offset2.xy * p2.w, 0.0, 0.0);
    EmitVertex();
    gl_Position = p2 - vec4(offset2.xy * p2.w, 0.0, 0.0);
    EmitVertex();

    EndPrimitive();
}
