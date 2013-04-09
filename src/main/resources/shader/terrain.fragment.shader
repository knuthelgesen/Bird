/***********************************************************************
Fragment shader for rendering terrain.

Procedure:

1.Set fragment color as defined in vertex shader
************************************************************************/

#version 120

void main() {
	//Set final fragment color as calculated in vertex shader
	gl_FragColor = gl_Color;
}
