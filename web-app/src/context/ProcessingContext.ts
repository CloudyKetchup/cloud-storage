import { Entity } from "../model/entity/Entity";

export interface ProcessingContext {
    entities : Entity[]
    add      : (entity : Entity) => Entity | null
    get      : (id : string) => Entity | null
    delete   : (id : string) => void
}