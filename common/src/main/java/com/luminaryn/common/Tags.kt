package com.luminaryn.common

import org.json.JSONArray

/**
 * A class designed to deal with tags.
 *
 * It can add, remove, and replace tags. It can also perform certain queries for tags.
 */
class Tags {

    var normalizeAddDefault: Boolean = false
    var normalizeHasDefault: Boolean = false
    var normalizeWantDefault: Boolean = false

    val children = ArrayList<Child>()

    /**
     * Build a new Tags object from a JSON array.
     *
     * @param input The JSON array we'll pass to addTags()
     * @param normalize If we should normalize the tag strings when adding them.
     */
    constructor(input: JSONArray, normalize: Boolean) {
        addTags(input, null, normalize)
    }

    /**
     * Build a new Tags object from a String of tags.
     *
     * @param input The String we'll pass to addTags()
     * @param normalize If we should normalize the tag strings when adding them.
     */
    constructor(input: String, normalize: Boolean)  {
        addTags(input, null, normalize)
    }

    /**
     * Return the children that are regular strings.
     * Does not include any tags within nested Tags, does not normalize results.
     */
    val tags: List<String>
        get() = getTags(false, 0)

    /**
     * Return the children that are Tags objects.
     */
    val sets: List<Tags>
        get() = children.filter { it.set != null }.map { it.set!! }

    /**
     * Return a list of tag strings.
     *
     * @param normalize If true, normalize the tags.
     * @param recurse If above zero, include tags in nested TagSet objects to the depth given here.
     *
     * @return The list of tags.
     */
    fun getTags(normalize: Boolean, recurse: Int): List<String> {
        if (recurse > 0) {
            val list = ArrayList<String>()
            for (child in children) {
                if (child.tag != null) {
                    val tag = if (normalize) normalize(child.tag) else child.tag
                    list.add(tag)
                } else if (child.set != null) {
                    list.addAll(child.set.getTags(normalize, recurse-1))
                }
            }
            return list.distinct()
        } else {
            val tags = children.filter { it.tag != null }.map { it.tag!! }
            return if (normalize) tags.map { normalize(it) } else tags
        }
    }

    /**
     * Return a JSON array representing the tags and nested Tags objects.
     *
     * Tags are included as simple strings, while Tags objects are included as
     * nested JSON arrays.
     */
    fun toJSON(): JSONArray {
        val json = JSONArray()
        for (child in children) {
            if (child.tag != null) {
                json.put(child.tag)
            } else if (child.set != null) {
                json.put(child.set.toJSON())
            }
        }
        return json
    }

    /**
     * Add a bunch of tags using a string as input.
     *
     * See the static fromString() method for details on how we split the strings.
     */
    @JvmOverloads
    fun addTags(inTags: String, pos: Int? = null, normalize: Boolean = normalizeAddDefault) {
        val tags = fromString(inTags, normalize).map { Child(it) }
        if (pos == null || pos == -1)
            children.addAll(tags)
        else
            children.addAll(pos, tags)
    }

    /**
     * Add a bunch of tags contained in another Tags object.
     *
     * This adds just the string tags. See mergeTags() for a version that merges all children,
     * including nested Tags objects.
     */
    @JvmOverloads
    fun addTags(inTags: Tags, pos: Int? = null, normalize: Boolean = normalizeAddDefault, recurse: Int = 0) {
        val tags = inTags.getTags(normalize, recurse).map { Child(it) }
        if (pos == null || pos == -1)
            children.addAll(tags)
        else
            children.addAll(pos, tags)
    }

    /**
     * Add a bunch of tags contained in a JSON array.
     *
     * Strings will be added as tags. Nested arrays will be added as nested Tags objects.
     */
    @JvmOverloads
    fun addTags(inTags: JSONArray, startPos: Int? = null, normalize: Boolean = normalizeAddDefault) {
        var pos = startPos
        for (i in 0 until inTags.length()) {
            val child = inTags.get(i)
            if (child is String && child.isNotEmpty()) {
                val tag = if (normalize) normalize(child) else child
                this.addTag(tag, pos, normalize)
            } else if (child is JSONArray) {
                val set = Tags(child, normalize)
                this.addSet(set, pos)
            }
            if (pos != null && pos != -1) pos++
        }
    }

    @JvmOverloads
    fun addTag(inTag: String, pos: Int? = null, normalize: Boolean = normalizeAddDefault) {
        val tag = if (normalize) normalize(inTag) else inTag
        val child = Child(tag)
        if (pos == null || pos == -1)
            children.add(child)
        else
            children.add(pos, child)
    }

    @JvmOverloads
    fun addSet(set: Tags, pos: Int? = null) {
        val child = Child(set)
        if (pos == null || pos == -1)
            children.add(child)
        else
            children.add(pos, child)
    }

    /**
     * Add all of the children from another Tags object.
     */
    @JvmOverloads
    fun mergeTags(inTags: Tags, pos: Int? = null) {
        if (pos == null || pos == -1)
            children.addAll(inTags.children)
        else
            children.addAll(pos, inTags.children)
    }

    /**
     * Replace a tag string with another tag string.
     */
    @JvmOverloads
    fun replace(oldTag: String, newTag: String, normalizeOld: Boolean = normalizeHasDefault, normalizeNew: Boolean = normalizeAddDefault) {
        val pos = indexOf(oldTag, normalizeOld, normalizeOld)
        if (pos != -1) removeAt(pos)
        addTag(newTag, pos, normalizeNew)
    }

    /**
     * Replace a tag string with a set of tag strings contained in another Tags object.
     */
    @JvmOverloads
    fun replace(oldTag: String, newTags: Tags, normalizeOld: Boolean = normalizeHasDefault, normalizeNew: Boolean = normalizeAddDefault) {
        val pos = indexOf(oldTag, normalizeOld, normalizeOld)
        if (pos != -1) removeAt(pos)
        addTags(newTags, pos, normalizeNew)
    }

    @JvmOverloads
    fun indexOf(wantTag: String, normalizeWant: Boolean = normalizeWantDefault, normalizeHas: Boolean = normalizeHasDefault): Int {
        val tag = if (normalizeWant) normalize(wantTag) else wantTag
        return children.indexOfFirst {
            if (it.tag != null) {
                val has = if(normalizeHas) normalize(it.tag) else it.tag
                has == tag
            } else false
        }
    }

    fun indexOf(set: Tags): Int {
        return children.indexOfFirst { it.set == set }
    }

    fun getChildFor(wantTag: String, normalizeWant: Boolean = normalizeWantDefault, normalizeHas: Boolean = normalizeHasDefault): Child? {
        val pos = indexOf(wantTag, normalizeWant, normalizeHas)
        if (pos == -1) return null
        return children[pos]
    }

    fun getChildFor(set: Tags): Child? {
        return children.find { it.set == set }
    }

    @JvmOverloads
    fun has(wantTag: String,
            normalizeWant: Boolean = normalizeWantDefault,
            normalizeHas: Boolean = normalizeHasDefault,
            recurseSets: Int = 0): Boolean {

        val tag = if (normalizeWant) normalize(wantTag) else wantTag

        //LongLog.v(TAG, "has($tag[$wantTag], $normalizeWant, $normalizeHas, $recurseSets)")

        if (indexOf(tag, false, normalizeHas) != -1) {
            // We have the tag in our direct list.
            return true
        } else if (recurseSets > 0) {
            for (set in sets) {
                if (set.has(tag, false, normalizeHas, recurseSets-1)) {
                    return true
                }
            }
        }

        // If we reached here, nothing else matched.
        return false
    }

    /**
     * Given another Tags object, see if we have ANY of the tags contained within it.
     * Nested Tags within the passed Tags object will be passed to the all() method.
     */
    @JvmOverloads
    fun any(wantTags: Tags,
            normalizeWant: Boolean = normalizeWantDefault,
            normalizeHas: Boolean = normalizeHasDefault,
            recurseHas: Int = 0): Boolean {

        //LongLog.v(TAG, "any(${wantTags.toJSON()}, $normalizeWant, $normalizeHas, $recurseHas)")

        for (child in wantTags.children) {
            if (child.set != null && all(child.set, normalizeWant, normalizeHas, recurseHas)) {
                return true // Nested all() tag-set matched.
            }
            else if (child.tag != null && has(child.tag, normalizeWant, normalizeHas, recurseHas)) {
                return true // We had one of the tags.
            }
        }

        // No tags matched.
        return false
    }

    /**
     * Given another Tags object, see if we have ALL of the tags contained within it.
     * Nested Tags within the passed Tags object will be passed to the any() method.
     */
    @JvmOverloads
    fun all(wantTags: Tags,
            normalizeWant: Boolean = normalizeWantDefault,
            normalizeHas: Boolean = normalizeHasDefault,
            recurseHas: Int = 0): Boolean {

        //LongLog.v(TAG, "all(${wantTags.toJSON()}, $normalizeWant, $normalizeHas, $recurseHas)")

        for (child in wantTags.children) {
            if (child.set != null && !any(child.set, normalizeWant, normalizeHas, recurseHas)) {
                return false // A nested any() tag-set did not match.
            } else if (child.tag != null && !has(child.tag, normalizeWant, normalizeHas, recurseHas)) {
                return false // We did not have one of the tags.
            }
        }

        // All tags matched.
        return true
    }

    fun removeAt(pos: Int): Child {
        return children.removeAt(pos)
    }

    fun remove(child: Child): Boolean {
        return children.remove(child)
    }

    fun remove(tag: String, normalizeWant: Boolean = normalizeWantDefault, normalizeHas: Boolean = normalizeHasDefault): Boolean {
        val child = getChildFor(tag, normalizeWant, normalizeHas)
        if (child != null) {
            return remove(child)
        }
        return false
    }

    fun remove(set: Tags): Boolean {
        val child = getChildFor(set)
        if (child != null) {
            return remove(child)
        }
        return false
    }

    class Child {
        val tag: String?
        val set: Tags?

        constructor(tag: String) {
            this.tag = tag
            set = null
        }

        constructor(set: Tags) {
            this.set = set
            tag = null
        }
    }

    companion object {
        const val TAG = "lum.tags"

        fun fromString(tagString: String, normalize: Boolean): List<String> {
            val tags = tagString.split(Regex("[\\s;,]+"))
            return if (normalize) tags.map { normalize(it) } else tags
        }

        fun normalize(tag: String): String {
            return tag.lowercase().removePrefix("#")
        }
    } // companion object

}