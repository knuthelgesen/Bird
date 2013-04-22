/***********************************************************************
Fragment shader for rendering terrain.

Procedure:

1.Set fragment color as defined in vertex shader
************************************************************************/

#version 120

uniform sampler3D tex;

varying float noiseVal;

void main() {
	vec4 texel;
	
	texel = texture3D(tex, gl_TexCoord[0].stp);

	//Set final fragment color as calculated in vertex shader
	gl_FragColor = gl_Color * texel * noiseVal;
}
