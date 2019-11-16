import { Entity } from "../model/entity/Entity";

export interface ProcessingContext {
    entities : Entity[]
	add      : (entity : Entity) => void
    get      : (id : string) => Entity | null
    delete   : (id : string) => void
}
