import { Component, createContext } from "react";
import { FileEntity } from "../model/entity/FileEntity";
import { FolderEntity } from "../model/entity/FolderEntity";
import { Entity } from "../model/entity/Entity";

export type ContentContextType = {
    files : FileEntity[]
    folders : FolderEntity[]
    trashItems : Entity[]
    setFiles : (newFiles : FileEntity[]) => FileEntity[]
    setFolders : (newFolders : FolderEntity[]) => FolderEntity[]
    setTrashItems : (newTrashItems : Entity[]) => Entity[]
};

