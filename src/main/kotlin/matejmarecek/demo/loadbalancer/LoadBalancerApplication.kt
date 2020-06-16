package matejmarecek.demo.loadbalancer

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

/**
 * The main entry point of the application.
 */
@SpringBootApplication
class LoadBalancerApplication

fun main(args: Array<String>) {
	runApplication<LoadBalancerApplication>(*args)
}
