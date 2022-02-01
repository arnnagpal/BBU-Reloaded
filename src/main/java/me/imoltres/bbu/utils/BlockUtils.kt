package me.imoltres.bbu.utils

import me.imoltres.bbu.BBU
import org.bukkit.Material
import org.bukkit.block.Block
import org.bukkit.block.BlockFace


class BlockUtils {

    companion object {
        val faces = BlockFace.values().toSet().minus(BlockFace.SELF)

        @JvmStatic
        fun facesTouching(faces: Set<BlockFace>, block: Block): Set<Block> {
            val facesTouching = HashSet<Block>()
            for (face in faces) {
                val b = block.getRelative(face)
                if (!b.isEmpty) {
                    facesTouching.add(b)
                }
            }

            return facesTouching
        }

        fun generatesCobble(faces: Set<BlockFace>, originalType: Material, toBlock: Block): Boolean {
            val mirrorType = if (originalType == Material.WATER) Material.LAVA else Material.WATER
            var gensCobble = false
            var cobbleBlock: Block? = null

            for (face in faces) {
                val r = toBlock.getRelative(face, 1)
                if (r.type == mirrorType) {
                    gensCobble = true
                    cobbleBlock = r
                    break
                }
            }

            if (gensCobble) {
                for (face in faces) {
                    val beacon = cobbleBlock!!.getRelative(face)
                    if (BBU.instance.teamController.getTeam(beacon) != null) {
                        return true
                    }
                }
            }
            return false
        }

    }

}