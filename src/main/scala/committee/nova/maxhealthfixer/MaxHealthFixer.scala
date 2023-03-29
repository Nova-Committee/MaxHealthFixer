package committee.nova.maxhealthfixer

import cpw.mods.fml.common.Mod.EventHandler
import cpw.mods.fml.common.event.FMLPreInitializationEvent
import cpw.mods.fml.common.eventhandler.SubscribeEvent
import cpw.mods.fml.common.gameevent.PlayerEvent.{PlayerLoggedInEvent, PlayerLoggedOutEvent}
import cpw.mods.fml.common.gameevent.TickEvent.{Phase, WorldTickEvent}
import cpw.mods.fml.common.{FMLCommonHandler, Mod}
import cpw.mods.fml.relauncher.Side
import net.minecraft.entity.player.EntityPlayerMP

import scala.collection.JavaConversions.asJavaIterator
import scala.collection.mutable

@Mod(modid = "maxhealthfixer", useMetadata = true, modLanguage = "scala")
object MaxHealthFixer {
  val set = new mutable.HashSet[EntityPlayerMP]()
  final val prevHealth = "prevHealth"

  @EventHandler
  def preInit(e: FMLPreInitializationEvent): Unit = if (e.getSide == Side.SERVER) FMLCommonHandler.instance().bus().register(new FMLEventHandler)

  class FMLEventHandler {
    @SubscribeEvent
    def onLogin(e: PlayerLoggedInEvent): Unit = {
      e.player match {
        case p: EntityPlayerMP => set.add(p)
        case _ =>
      }
    }

    @SubscribeEvent
    def onLogout(e: PlayerLoggedOutEvent): Unit = {
      e.player match {
        case p: EntityPlayerMP => p.getEntityData.setFloat(prevHealth, p.getHealth)
        case _ =>
      }
    }

    @SubscribeEvent
    def onWorldTick(e: WorldTickEvent): Unit = {
      if (e.phase == Phase.START || set.isEmpty) return
      val itr = set.iterator
      while (itr.hasNext) {
        val p = itr.next
        itr.remove()
        val data = p.getEntityData
        if (data.hasKey(prevHealth)) {
          val stored = data.getFloat(prevHealth)
          data.removeTag(prevHealth)
          val current = p.getHealth
          if (stored > current) p.setHealth(stored min p.getMaxHealth)
          set.remove(p)
        }
      }
    }
  }
}