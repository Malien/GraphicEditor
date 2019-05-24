#version 120

attribute vec3 a_position;
attribute vec4 a_color;

uniform mat3 u_matrix;

varying vec4 v_color;

void main() {
  // Multiply the position by the matrix.
  gl_Position = vec4(u_matrix * a_position, 1.0);

  // Pass the color to the fragment shader.
  v_color = a_color;
}