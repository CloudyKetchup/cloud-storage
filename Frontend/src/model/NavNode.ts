export default class NavNode {
	
	id: number;
	name: string;
	onClick: () => void;
	prev: NavNode | undefined;
	next: NavNode | null = null;

	constructor(id: number, name: string, prev: NavNode | undefined, onClick: () => void) {
		this.id 	= id;
		this.name	= name;
		this.onClick	= onClick;
		this.prev	= prev;
	}
}
