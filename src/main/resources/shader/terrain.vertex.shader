/***********************************************************************
Vertex shader for rendering for mouse picking.

Procedure:

1.Calculate final position based on camera matrix
2.Set color to red (for testing)
************************************************************************/

#version 120

void main() {
	/**************************************************
	Vertex operations starts here
	***************************************************/
	//Use the ModelViewProjectionMatrix to calculate world space position
	gl_Position = gl_ModelViewProjectionMatrix*gl_Vertex;
	
	//gl_Position[1] = gl_Position[1] - 0.00005 * (gl_Position[2] * gl_Position[2]); 
	
	//Set the color	
	gl_FrontColor = vec4(gl_Normal[1], gl_Normal[1], gl_Normal[1], 1.0);
	//gl_FrontColor = vec4(1.0, 1.0, 1.0, 1.0);
}
	