import { FileEntity } from "../model/entity/FileEntity";
import { FolderEntity } from "../model/entity/FolderEntity";
import { Entity } from "../model/entity/Entity";

export type BasicContentContext = {
    files : FileEntity[]
    folders : FolderEntity[]
    setFiles: (newFiles: FileEntity[]) => FileEntity[]
    setFolders : (newFolders : FolderEntity[]) => FolderEntity[]
};

export interface ContentContextInterface extends BasicContentContext {
    trashItems : Entity[]
    setTrashItems : (newTrashItems : Entity[]) => Entity[]
};