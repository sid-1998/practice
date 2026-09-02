package com.practice.recommendation.model;

/**
 * A single carousel tile: a show plus the content needed to render it.
 *
 * <p>Constructed only by the content layer from content it already holds, so it does not
 * re-validate — input validation happens at the request boundary.
 */
public record Show(String id, String title, String description, String imageUrl) {
}
