package eu.bitwalker.symasset.service

import net.sympower.control.api.server.models.Resource
import net.sympower.control.api.server.models.ResourceGroup

class ResourceGroupService {

    private val resourceGroups = sortedMapOf<Int, ResourceGroup>()

    fun addResourceGroup(resourceGroup: ResourceGroup) {
        resourceGroups[resourceGroup.number] = resourceGroup
    }

    fun addResource(resource: Resource) {
        var resourceGroup =  resourceGroups[resource.address.groupNumber]!!
        resourceGroup.resources = resourceGroup.resources?.plus(resource)
    }

    fun getByNumber(id: Int?): ResourceGroup {
        return resourceGroups[id]!!
    }

    fun getAll(): MutableCollection<ResourceGroup> {
        return resourceGroups.values
    }

}