package androidx.compose.remote.core.operations.layout.animation

import androidx.compose.remote.core.PaintContext
import androidx.compose.remote.core.operations.layout.Component
import androidx.compose.remote.core.operations.layout.measure.ComponentMeasure
import androidx.compose.remote.core.operations.paint.PaintBundle
import kotlin.random.Random

class ParticleAnimation {
    private val mAllParticles: HashMap<Int, ArrayList<Particle>> = HashMap()
    private val mPaint = PaintBundle()

    fun animate(
        context: PaintContext,
        component: Component,
        start: ComponentMeasure,
        end: ComponentMeasure,
        progress: Float
    ) {
        val particles = mAllParticles.getOrPut(component.componentId) {
            ArrayList<Particle>().apply {
                repeat(20) {
                    add(Particle(
                        Random.nextFloat(), Random.nextFloat(), Random.nextFloat(),
                        220f, 220f, 220f
                    ))
                }
            }
        }
        context.save()
        context.savePaint()
        for (particle in particles) {
            mPaint.reset()
            mPaint.setColor(
                particle.r / 255f,
                particle.g / 255f,
                particle.b / 255f,
                (200 * (1 - progress)) / 255f
            )
            context.applyPaint(mPaint)
            val dx = start.x + component.width * particle.x
            val dy = start.y + component.height * particle.y + progress * 0.01f * component.height
            val dr = (component.height + 60) * 0.15f * particle.radius + (30 * progress)
            context.drawCircle(dx, dy, dr)
        }
        context.restorePaint()
        context.restore()
    }
}
