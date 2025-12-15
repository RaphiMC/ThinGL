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

    // Clip line segments that are behind the camera
    if (p1.w * p2.w < 0.0) {
        if (p1.w < 0.0) {
            p1 = mix(p1, p2, -p1.w / (p2.w - p1.w));
        } else {
            p2 = mix(p2, p1, -p2.w / (p1.w - p2.w));
        }
    }

    vec2 dir = normalize((p2.xy / p2.w - p1.xy / p1.w) * u_Viewport);
    vec2 normal = vec2(-dir.y, dir.x);

    vec4 offset1 = vec4(normal * v_LineWidth[0] / u_Viewport * p1.w, 0.0, 0.0);
    g_Color = v_Color[0];
    gl_Position = p1 + offset1;
    EmitVertex();
    gl_Position = p1 - offset1;
    EmitVertex();

    vec4 offset2 = vec4(normal * v_LineWidth[1] / u_Viewport * p2.w, 0.0, 0.0);
    g_Color = v_Color[1];
    gl_Position = p2 + offset2;
    EmitVertex();
    gl_Position = p2 - offset2;
    EmitVertex();

    EndPrimitive();
}
