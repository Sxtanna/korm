package com.sxtanna.korm.data.custom

/**
 * Classes annotated with this will be pulled and pushed as a list, instead of a hash
 *
 * ```
 * {
 *   name: "Name"
 *   date: MONDAY
 * }
 * ```
 *
 * **becomes**
 *
 * ```["Name", MONDAY]```
 *
 * @param props The list of the properties to be serialized in order
 *
 */
@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
annotation class KormList(val props: Array<String>)