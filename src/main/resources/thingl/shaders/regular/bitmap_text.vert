#version 430 core

struct StringData {
    uint textColor;
};
struct CharData {
    uint textureAndStringIndex;
};

uniform mat4 u_ProjectionMatrix;
uniform mat4 u_ViewMatrix;
uniform mat4 u_ModelMatrix;

layout (std430) readonly buffer ssbo_StringData {
    StringData stringDatas[];
};
layout (std430) readonly buffer ssbo_CharData {
    CharData charDatas[];
};

layout (location = 0) in vec3 i_Position;
layout (location = 1) in vec2 i_TexCoords;
out vec2 v_TexCoords;
flat out uint v_TextureIndex;
flat out vec4 v_TextColor;

void main() {
    gl_Position = u_ProjectionMatrix * u_ViewMatrix * u_ModelMatrix * vec4(i_Position, 1.0);

    CharData charData = charDatas[gl_VertexID / 4];
    StringData stringData = stringDatas[charData.textureAndStringIndex & 0x7FFFFFF];

    v_TexCoords = i_TexCoords;
    v_TextureIndex = (charData.textureAndStringIndex >> 27) & 31;
    v_TextColor = unpackUnorm4x8(stringData.textColor);
}
