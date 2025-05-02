// service.ts
export class Service {
    // Pets
    async getPets(request, reply) {
      const pets = [
        { id: 1, name: 'Fluffy', tag: 'cat' },
        { id: 2, name: 'Rex', tag: 'dog' },
        { id: 3, name: 'Bella', tag: 'cat' }
      ];
      
      // Handle any query parameters from the request
      const { limit, tags } = request.query;
      
      let result = [...pets];
      if (tags) {
        const tagList = tags.split(',');
        result = result.filter(pet => tagList.includes(pet.tag));
      }
      
      if (limit && !isNaN(parseInt(limit))) {
        result = result.slice(0, parseInt(limit));
      }
      
      return result;
    }
  
    async getPetById(request, reply) {
      const pets = [
        { id: 1, name: 'Fluffy', tag: 'cat' },
        { id: 2, name: 'Rex', tag: 'dog' },
        { id: 3, name: 'Bella', tag: 'cat' }
      ];
      
      const petId = parseInt(request.params.id);
      const pet = pets.find(p => p.id === petId);
      
      if (!pet) {
        return reply.code(404).send({ error: 'Pet not found' });
      }
      
      return pet;
    }
  
    async addPet(request, reply) {
      // In a real app, this would add to a database
      const newPet = request.body;
      
      // Simulate adding an ID
      newPet.id = Date.now();
      
      return reply.code(201).send(newPet);
    }
  
    async updatePet(request, reply) {
      const petId = parseInt(request.params.id);
      const updatedPet = { ...request.body, id: petId };
      
      // In a real app, this would update a database
      return updatedPet;
    }
  
    async deletePet(request, reply) {
      const petId = parseInt(request.params.id);
      
      // In a real app, this would delete from a database
      return reply.code(204).send();
    }
  }
  
  export default Service;