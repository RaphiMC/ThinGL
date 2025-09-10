#version 430 core

struct TextData {
    uint textColor;
};
struct GlyphData {
    uint textureAndTextIndex;
};

uniform mat4 u_ProjectionMatrix;
uniform mat4 u_ViewMatrix;
uniform mat4 u_ModelMatrix;

layout (std430) restrict readonly buffer ssbo_TextData {
    TextData textDatas[];
};
layout (std430) restrict readonly buffer ssbo_GlyphData {
    GlyphData glyphDatas[];
};

layout (location = 0) in vec3 a_Position;
layout (location = 1) in vec2 a_TexCoord;
out vec2 v_TexCoord;
flat out uint v_TextureIndex;
flat out vec4 v_TextColor;

void main() {
    gl_Position = u_ProjectionMatrix * u_ViewMatrix * u_ModelMatrix * vec4(a_Position, 1);

    GlyphData glyphData = glyphDatas[gl_VertexID / 4];
    TextData textData = textDatas[glyphData.textureAndTextIndex & 0x7FFFFFFu];

    v_TexCoord = a_TexCoord;
    v_TextureIndex = (glyphData.textureAndTextIndex >> 27) & 31u;
    v_TextColor = unpackUnorm4x8(textData.textColor);
}
