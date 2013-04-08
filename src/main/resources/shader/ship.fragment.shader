/***********************************************************************
Fragment shader for rendering ships.

Procedure:

1.Set fragment color from texture
2.Set fragment color as defined in vertex shader to add light
************************************************************************/

#version 120

uniform sampler2D textureId;

varying vec4 color;

void main() {
	//Set final fragment color as calculated in vertex shader
	gl_FragColor = texture2D(textureId, gl_TexCoord[0].st);

	gl_FragColor = vec4(gl_FragColor * color);
	
	gl_FragColor[0] = (round(gl_FragColor[0] * 10)) / 10;
	gl_FragColor[1] = (round(gl_FragColor[1] * 10)) / 10;
	gl_FragColor[2] = (round(gl_FragColor[2] * 10)) / 10;
	
}
