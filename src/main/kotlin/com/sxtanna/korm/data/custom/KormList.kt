package com.sxtanna.korm.data.custom

/**
 * Used to signify that a type should be serialized as a list of values instead of a hash
 *
 *
 * ```
 * {
 *   name: "Name"
 *   date: MONDAY
 * }
 * ```
 * becomes
 *
 * ```
 * ["Name", MONDAY]
 * ```
 *
 * @param props The list of the properties to be serialized in order
 *
 */
@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
annotation class KormList(val props: Array<String>)