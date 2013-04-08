/***********************************************************************
Fragment shader for rendering for picking.

Procedure:

1.Set fragment color as defined in vertex shader
************************************************************************/

#version 120

uniform sampler2D textureId;

varying vec4 color;

void main() {
	//Set final fragment color as calculated in vertex shader
	gl_FragColor = gl_Color;
}
