using System;
using Microsoft.EntityFrameworkCore.Migrations;

#nullable disable

namespace LampControlApi.Infrastructure.Database.Migrations
{
    /// <inheritdoc />
    public partial class InitialCreate : Migration
    {
        /// <inheritdoc />
        protected override void Up(MigrationBuilder migrationBuilder)
        {
            migrationBuilder.CreateTable(
                name: "lamps",
                columns: table => new
                {
                    id = table.Column<Guid>(type: "uuid", nullable: false),
                    is_on = table.Column<bool>(type: "boolean", nullable: false),
                    created_at = table.Column<DateTimeOffset>(type: "timestamp with time zone", nullable: false),
                    updated_at = table.Column<DateTimeOffset>(type: "timestamp with time zone", nullable: false),
                    deleted_at = table.Column<DateTimeOffset>(type: "timestamp with time zone", nullable: true)
                },
                constraints: table =>
                {
                    table.PrimaryKey("PK_lamps", x => x.id);
                });

            migrationBuilder.CreateIndex(
                name: "idx_lamps_created_at",
                table: "lamps",
                column: "created_at");

            migrationBuilder.CreateIndex(
                name: "idx_lamps_deleted_at",
                table: "lamps",
                column: "deleted_at");

            migrationBuilder.CreateIndex(
                name: "idx_lamps_is_on",
                table: "lamps",
                column: "is_on");
        }

        /// <inheritdoc />
        protected override void Down(MigrationBuilder migrationBuilder)
        {
            migrationBuilder.DropTable(
                name: "lamps");
        }
    }
}
