#version 430 core

struct TextData {
    uint fontSize;
    uint textColor;
    uint outlineColor;
    uint styleFlags;
};
struct GlyphData {
    uint textureAndTextIndex;
};

uniform mat4 u_ProjectionMatrix;
uniform mat4 u_ViewMatrix;
uniform mat4 u_ModelMatrix;
uniform vec2 u_Viewport;

layout (std430) restrict readonly buffer ssbo_TextData {
    TextData textDatas[];
};
layout (std430) restrict readonly buffer ssbo_GlyphData {
    GlyphData glyphDatas[];
};

layout (location = 0) in vec3 i_Position;
layout (location = 1) in vec2 i_TexCoords;
out vec2 v_TexCoords;
flat out uint v_TextureIndex;
flat out uint v_FontSize;
flat out vec4 v_TextColor;
flat out vec4 v_OutlineColor;
flat out uint v_StyleFlags;

void main() {
    gl_Position = u_ProjectionMatrix * u_ViewMatrix * u_ModelMatrix * vec4(i_Position, 1);

    GlyphData glyphData = glyphDatas[gl_VertexID / 4];
    TextData textData = textDatas[glyphData.textureAndTextIndex & uint(0x7FFFFFF)];

    v_TexCoords = i_TexCoords;
    v_TextureIndex = (glyphData.textureAndTextIndex >> 27) & uint(31);
    v_FontSize = textData.fontSize;
    v_TextColor = unpackUnorm4x8(textData.textColor);
    v_OutlineColor = unpackUnorm4x8(textData.outlineColor);
    v_StyleFlags = textData.styleFlags;
}
