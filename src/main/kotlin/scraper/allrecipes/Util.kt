package scraper.allrecipes

fun <T, R> Iterable<T>.flatMapIndexed(transform: (index: Int, T) -> Iterable<R>): List<R> {
    return flatMapIndexedTo(ArrayList(), transform)
}

inline fun <T, R, C : MutableCollection<in R>> Iterable<T>.flatMapIndexedTo(destination: C, transform: (index: Int, T) -> Iterable<R>): C {
    var index = 0
    for (item in this)
        destination.addAll(transform(index++, item))
    return destination
}