/**
 * Application layer (use cases / business rules).
 * <p>
 * {@code port.in}  - use-case interfaces called by the presentation layer.<br>
 * {@code port.out} - interfaces the use cases need (PokeAPI gateway, repositories),
 *                    implemented by the infrastructure layer.<br>
 * {@code usecase}  - implementations of the inbound ports.
 * <p>
 * Depends only on {@code domain}. Never on infrastructure or presentation.
 */
package com.pokedex.application;
