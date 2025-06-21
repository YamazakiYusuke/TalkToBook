package com.example.talktobook.data.mapper

interface EntityMapper<Entity, Domain> {
    
    fun Entity.toDomainModel(): Domain
    
    fun Domain.toEntity(): Entity
    
    fun List<Entity>.toDomainModels(): List<Domain> = map { it.toDomainModel() }
    
    fun List<Domain>.toEntities(): List<Entity> = map { it.toEntity() }
}