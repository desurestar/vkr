package ru.zagrebin.front_mobile.data.local

import org.junit.Assert.assertEquals
import org.junit.Test
import ru.zagrebin.front_mobile.domain.model.RecipeIngredient
import ru.zagrebin.front_mobile.domain.model.RecipeStep
import ru.zagrebin.front_mobile.domain.model.RecipeTag

class ConvertersTest {
    private val converters = Converters()

    @Test
    fun tags_roundTrip() {
        val input = listOf(RecipeTag(1, "#tag"), RecipeTag(2, "#test"))
        val json = converters.fromTags(input)
        val output = converters.toTags(json)
        assertEquals(input, output)
    }

    @Test
    fun ingredients_roundTrip() {
        val input = listOf(RecipeIngredient("Apple"), RecipeIngredient("Milk"))
        val json = converters.fromIngredients(input)
        val output = converters.toIngredients(json)
        assertEquals(input, output)
    }

    @Test
    fun steps_roundTrip() {
        val input = listOf(
            RecipeStep(1, "Step 1", "Do it", null),
            RecipeStep(2, "Step 2", "Done", "url")
        )
        val json = converters.fromSteps(input)
        val output = converters.toSteps(json)
        assertEquals(input, output)
    }
}

